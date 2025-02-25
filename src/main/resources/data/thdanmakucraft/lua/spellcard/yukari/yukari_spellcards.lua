---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2025/2/16 上午 12:31
---
--------------------------------------------------------------------------------------------------
---@type Class|THBullet
local yukari_spellcrad_1_bullet_1 = core.defineClass()
function yukari_spellcrad_1_bullet_1:onInit(_pos,_angle)
    self:setStyle(bullet_styles.ball_mid)
    self:setPosition(_pos)
    self:setVelocity(_angle:scale(0.2),true)
    self:setAccelerationFromDirection(0.03,_angle)
    self:setLifetime(120)
    self:setBlend("add")
end

function yukari_spellcrad_1_bullet_1:onTick()
end
--------------------------------------------------------------------------------------------------
---@type Class|THObjectContainer
local yukari_spellcrad_1 = core.defineSpellCardClass("yukari_spellcard_1")
function yukari_spellcrad_1:onInit()
    self:setSpellCardName("Border Sign \"Boundary of Truth and Falsehood\"")
    self:setLifetime(300)
end

function yukari_spellcrad_1:onTick()
    local timer = self:getTimer()
    ---@param _pos util.Vec3
    ---@param _angle util.Vec3
    local bbb = function(_pos,_angle)
        self:createTHBullet(yukari_spellcrad_1_bullet_1,
                {_pos:add(_angle:scale(math.min(timer/120,1)^0.4*1.6)),_angle},
                _pos,bullet_styles.grain_a,3)
    end

    if timer < 300 then
        local pos = self:getPosition()
        local rotation = util.vec3.new(0.0,0.0,1.0)
        local way = 6
        local rotate = util.vec2.new(
                Mth.DEG_TO_RAD*(math.pow(timer*0.1,2)+360/way),
                -Mth.DEG_TO_RAD*(math.pow(timer*0.08,2)+360/way))

        local angle = rotation:xRot(Mth.DEG_TO_RAD*90):xRot(rotate.x):yRot(rotate.y)
        bbb(pos,angle)

        local way2 = 4
        for i=1,(way2-1) do
            local angle2 = rotation:xRot(Mth.DEG_TO_RAD*90-Mth.DEG_TO_RAD*(180/way2)*i):yRot(Mth.DEG_TO_RAD*(180/way)*i)
            for j=0,(way-1) do
                local angle3 = angle2:yRot(-Mth.DEG_TO_RAD*(360/way)*j):normalize():xRot(rotate.x):yRot(rotate.y)
                bbb(pos,angle3)
            end
        end
        local angle3 = rotation:xRot(Mth.DEG_TO_RAD*90-Mth.DEG_TO_RAD*180):xRot(rotate.x):yRot(rotate.y)
        bbb(pos,angle3)
    end
end
--------------------------------------------------------------------------------------------------
---@type Class|THLaser
local yukari_spellcrad_2_laser_1 = core.defineClass()
function yukari_spellcrad_2_laser_1:onInit(_pos0,_pos1,_pos2,_width)
    self.data = {}
    self.autosave:register("data")
    self.data.pos0 = _pos0
    self.data.pos1 = _pos1
    self.data.pos2 = _pos2
    self.data.width = _width
    self:setWidth(0)
    self:setLaserColorByIndex(6)
    self:setLifetime(300)
    self:setCollision(false)
end

function yukari_spellcrad_2_laser_1:onTick()
    local radius = 5.0
    local angle = self.container.data.angle
    local pos0 = self.data.pos0
    local pos1 = self.data.pos1
    local pos2 = self.data.pos2
    self:setPosition(pos0:add(pos1:yRot(-angle)))
    self:vectorTo(pos2:yRot(angle))

    local mod = (self.timer+240)%240
    if mod == 0 then
        self:growWidth(self.data.width,60)
    end

    if mod == 20 then
        self:setCollision(true)
    end

    if mod == 120 then
        self:growWidth(0.01,30)
        self:setCollision(false)
    end

    --print(self.timer)
end

---@type Class|THLaser
local yukari_spellcrad_2_laser_2 = core.defineClass()
function yukari_spellcrad_2_laser_2:onInit(_pos0,_pos1,_pos2,_width)
    self.data = {}
    self.data.pos0 = _pos0
    self.data.pos1 = _pos1
    self.data.pos2 = _pos2
    self.data.width = _width
    self.autosave:register("data")
    self:setWidth(0)
    self:setLaserColorByIndex(2)
    self:setLifetime(300)
end

function yukari_spellcrad_2_laser_2:onTick()
    local radius = 5.0
    local angle = -self.container.data.angle*3
    local pos0 = self.data.pos0
    local pos1 = self.data.pos1
    local pos2 = self.data.pos2
    self:setPosition(pos0:add(pos1:yRot(-angle)))
    self:vectorTo(pos2:yRot(angle))

    local mod = (self.timer+240)%240
    if mod == 0 then
        self:growWidth(0.01,30)
        self:setCollision(false)
    end

    if mod == 120 then
        self:growWidth(self.data.width,60)
    end

    if mod == 120+20 then
        self:setCollision(true)
    end
end

---@type Class|THObjectContainer
local yukari_spellcrad_2 = core.defineSpellCardClass("yukari_spellcard_2")
function yukari_spellcrad_2:onInit()
    self:setSpellCardName("Evil Spirits \"Butterfly in the Zen Temple\"")
    self:setLifetime(300)
    self.data = {}
    self.autosave:register("data")
    self.data.angle = 0
    for i=0,3 do
        local pos0 = self:getPosition()
        local pos = util.vec3.new(10,0,0):yRot(-Mth.DEG_TO_RAD*90*i)
        self:createTHLaser(yukari_spellcrad_2_laser_1,
                {pos0,util.vec3.new(0,0,0),pos,1.0}
        )

        self:createTHLaser(yukari_spellcrad_2_laser_1,
                {pos0,pos:yRot(-Mth.DEG_TO_RAD*90 * ((i+3)%2-0.5)*2), pos,1.0}
        )

        local pos2 = util.vec3.new(20,0,0):yRot(-Mth.DEG_TO_RAD*90*i)
        self:createTHLaser(yukari_spellcrad_2_laser_2,
                {pos0,util.vec3.new(0,0,0),pos2,1.0}
        )

        self:createTHLaser(yukari_spellcrad_2_laser_2,
                {pos0,pos2:yRot(-Mth.DEG_TO_RAD*90 * ((i+3)%2-0.5)*2), pos2,1.0}
        )
    end
end

function yukari_spellcrad_2:onTick()
    self.data.angle = self.timer/60
end
--------------------------------------------------------------------------------------------------