local num = 3

---@type Class|THBullet
local testBullet = core.registerClass()
function testBullet:onInit(i)
    self:setVelocity(core.vec3(0.2,0.0,0.0):yRot(math.pi*2/32*i),true)
    self:setNavi(true)
end

function testBullet:onTick()
    self:move(0.0,0.1,0.0)
end

---@type Class|THBullet
local testBullet2 = core.registerClass(testBullet)
function testBullet2:onInit(i)
    self.class.super.onInit(self,i)
end

function testBullet2:onTick()
    --print("fuck")
    self.class.super.onTick(self)
end

---@type Class|THCurvedLaser
local testLaser = core.registerClass()
function testLaser:onInit(i)
    self:setLifetime(600)
    self.parameterManager:register("Double","angle",360/num*i + 0.001)
    --local userRot = self:getContainer().parameterManager:getDouble("userAngle")
end

function testLaser:onTick()
    local userRot = self:getContainer().parameterManager:getDouble("userAngle")
    local angle = self.parameterManager:getDouble("angle")
    --local angle = self.angle
    self:setVelocityFromRotation(0.2,
            {
                0.0,
                angle + 60 * Mth.sin(self:getTimer() * 0.1) + userRot
            },
            true,
            true);
end


---@type Class|THObjectContainer
local container = core.registerClass("testContainer")
function container:onInit()
    local userRot = self:getUser():getRotation().y
    self.parameterManager:register("Double","userAngle",userRot)
    for i = 1,num  do
        self:createTHCurvedLaser(testLaser, {i}, self:getPosition(),1,12,0.5)
    end

    for i = 1,32 do
        self:createTHBullet(testBullet2, {i}, self:getPosition(),"arrow_big",1)
    end
end

function container:onTick()
end


local testBullet3 = core.registerClass()
function testBullet3:onInit()
    self:setLifetime(10000)
end


---@type Class|THObjectContainer
local container2 = core.registerClass("testContainer2")
function container2:onInit()
    self:createTHBullet(testBullet3, {i}, self:getPosition(),"arrow_big",1)
end

function container2:onTick()
end