local num = 3

---@type Class|THBullet
local testBullet = core.defineClass()
function testBullet:onInit(i)
    self:setVelocity(util.vec3.new(0.2,0.0,0.0):yRot(Mth.PI*2/32*i),true)
    self:setNavi(true)
end

function testBullet:onTick()
    self:move(0.0,0.1,0.0)
end

---@type Class|THBullet
local testBullet2 = core.defineClass(testBullet)
---@param taskManager TaskManager
function testBullet2:onRegisterTasks(taskManager)
    ---@param target THBullet
    taskManager:registerTask("test", function(target, timer, lifetime)
        target:move(util.vec3.new(0.0,-0.1,0.0))
    end)
    --print("onRegisterTasks")
end

function testBullet2:onInit(i)
    self.class.super.onInit(self,i)
    --print("testBullet2")
    self.taskManager:startTask("test",120)
end

function testBullet2:onTick()
    --self.class.super.onTick(self)
end

---@type Class|THCurvedLaser
local testLaser = core.defineClass()
function testLaser:onInit(i)
    self.autosave:register("params")
    self:setLifetime(600)
    self.params = {}
    self.params.angle = 360/num*i
    self:setWidth(0.01)
end

function testLaser:onAddTasks()
end

function testLaser:onTick()
    local userRot = self.container.userRot
    local angle = self.params.angle
    self:setVelocityFromRotation(0.2,
            util.vec2.new(
                    0.0,
                    angle + 60 * Mth.sin(self.timer * 0.1) + userRot
            ),
            true,
            true);

    for i=0,100 do
        local timer = self.timer
    end

    if self.timer == 40 then
        self:growWidth(0.5,120)
    end
end

function testLaser:onRemove()
end


---@type Class|THObjectContainer
local container = core.defineClass("testContainer")
function container:onConstruct()
    --self.autosave:register("sad")
    --self.autosave:register("userRot")
end

function container:onRegisterTasks()
    self.taskManager:registerTask("test", function(target, timer)
        print("sad"..timer)
    end)
end

function container:onInit()
    local userRot = 0
    self.autosave:register("sad")
    self.autosave:register("userRot")
    self.userRot = userRot
    for i = 1,num  do
        self:createTHCurvedLaser(testLaser, {i}, self:getPosition(),1,120,0.5)
    end

    for i = 1,32 do
        self:createTHBullet(testBullet2, {i}, self:getPosition(),bullet_styles.ball_big,1)
    end
    self.sad = "sdfhhsdfubnsdfusbdufd"
end

function container:onTick()
    --print(self.sad)
    if(self:getTimer() == 2) then
        --self.taskManager:startTask("test")
    end
end

---@type Class|THBullet
local testBullet3 = core.defineClass()
function testBullet3:onInit()
    self:setLifetime(10000)
end

---@type Class|THObjectContainer
local container2 = core.defineClass("testContainer2")
function container2:onInit()
    self:setSpellCardName("境符「波與粒的境界」")
    self:getMaxObjectAmount(10)
    self:setLifetime(300)
    self.taskManager:startTask("test",120)
    print("sad")
end

function container2:onTick()
    local timer = self:getTimer()

    ---@param _pos util.Vec3
    ---@param _angle util.Vec3
    local bbb = function(_pos,_angle)
        local bullet2 = self:createTHBullet(nil, {i}, _pos,"grain_a",3)
        bullet2:setPosition(_pos:add(_angle:scale(2.0)))
        bullet2:setVelocity(_angle:scale(0.2),true)
        bullet2:setAccelerationFromDirection(0.02,_angle)
        bullet2:setLifetime(120)
        bullet2:setBlend("add")
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
            for j=0,(way) do
                local angle3 = angle2:yRot(-Mth.DEG_TO_RAD*(360/way)*j):normalize():xRot(rotate.x):yRot(rotate.y)
                bbb(pos,angle3)
            end
        end
        local angle3 = rotation:xRot(Mth.DEG_TO_RAD*90-Mth.DEG_TO_RAD*180):xRot(rotate.x):yRot(rotate.y)
        bbb(pos,angle3)
    end
end