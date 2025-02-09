local num = 3

---@type Class|THBullet
local testBullet = core.defineClass()
function testBullet:onInit(i)
    self:setVelocity(util.vec3.new(0.2,0.0,0.0):yRot(math.pi*2/32*i),true)
    self:setNavi(true)
end

function testBullet:onTick()
    self:move(0.0,0.1,0.0)
end

---@type Class|THBullet
local testBullet2 = core.defineClass(testBullet)
function testBullet2:onInit(i)
    self.class.super.onInit(self,i)
end

function testBullet2:onTick()
    self.class.super.onTick(self)
end

---@type Class|THCurvedLaser
local testLaser = core.defineClass()
function testLaser:onInit(i)
    self:setLifetime(600)
    --self.parameterManager:define("Double","angle",360/num*i + 0.001)
    self.params.angle = 360/num*i + 0.001
    --local userRot = self:getContainer().parameterManager:getDouble("userAngle")
end

function testLaser:onAddTasks()
    --[[
    self.taskManager:addTask({co = coroutine.create(function(target)
        for i = 1, 100 do
            --print("sad"..i)
            self:move(0, -0.2, 0)
            --task.wait()
            coroutine.yield()
        end
    end)})]]
end

function testLaser:onTick()
    --self.taskManager:doTasks()
    --local userRot = self:getContainer().parameterManager:get("userAngle")
    local userRot = self.container.params.userRot
    local angle = self.params.angle
    --local angle = self.angle
    self:setVelocityFromRotation(0.2,
            util.vec2(
                    0.0,
            angle + 60 * Mth.sin(self:getTimer() * 0.1) + userRot
            ),
            true,
            true);
end

function testLaser:onRemove()
    --self.taskManager:clear()
end


---@type Class|THObjectContainer
local container = core.defineClass("testContainer2")
function container:onInit()
    local userRot = self:getUser():getRotation().y
    self.params.userRot = userRot
    for i = 1,num  do
        self:createTHCurvedLaser(testLaser, {i}, self:getPosition(),1,12,0.5)
    end

    for i = 1,32 do
        self:createTHBullet(testBullet2, {i}, self:getPosition(),"arrow_big",1)
    end
end

function container:onTick()
end

local testBullet3 = core.defineClass()
function testBullet3:onInit()
    self:setLifetime(10000)
end

---@type Class|THObjectContainer
local container2 = core.defineClass("testContainer")
function container2:onInit()
    --[[
    local position = self:getPosition()
    local sample = 10
    for i = 1,sample do
        for j = 1,sample/2 do
            --self:newTHObject()
            local bullet = self:createTHBullet(testBullet3, {i}, {x=0,y=0,z=0},"arrow_big",1)
            bullet:setPosition(position:add(util.vec3.new(2.0,0.0,0.0):yRot(math.pi*2/sample*i):xRot(math.pi*2/sample*j)))
            bullet:setStyle("ball_small")

        end
    end]]
end

function container2:onTick()
    local timer = self:getTimer()

    local bbb = function(_pos,_angle)
        local bullet2 = self:createTHBullet(nil, {i}, _pos,"arrow_big",6)
        bullet2:setVelocity(_angle:scale(0.2),true)
        bullet2:setAccelerationFromDirection(0.02,_angle)
        bullet2:setLifetime(120)
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
        for i=1,(1+3) do
            local angle2 = rotation:xRot(Mth.DEG_TO_RAD*90-Mth.DEG_TO_RAD*60*i):yRot(Mth.DEG_TO_RAD*(180/way)*i)
            for j=0,way do
                local angle3 = angle2:yRot(-Mth.DEG_TO_RAD*(360/way)*j):normalize():xRot(rotate.x):yRot(rotate.y)
                bbb(pos,angle3)
            end
        end
        local angle3 = rotation:xRot(Mth.DEG_TO_RAD*90-Mth.DEG_TO_RAD*180):xRot(rotate.x):yRot(rotate.y)
        bbb(pos,angle3)
    end
end