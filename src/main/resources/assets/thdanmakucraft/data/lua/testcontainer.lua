local num = 1

---@type THBullet
local testBullet = core.registerClass("testBullet")
function testBullet:onInit(i)
    self.setVelocity(core.newVec3(0.2,0.0,0.0):yRot(math.pi*2/32*i),true)
    self.spawn()
    self.setNavi(true)
end

function testBullet:onTick()
    ---self.move(0.0,0.1,0.0)
end

---@type THCurvedLaser
local testLaser = core.registerClass("testLaser")
function testLaser:onInit(i)
    self.setLifetime(600)
    self.getParameterManager().register("Double","angle",360/num*i + 0.001)
    self.spawn()
end

function testLaser:onTick()
    local userRot = self.getContainer().getParameterManager().getDouble("userAngle")
    local angle = self.getParameterManager().getDouble("angle")
    self.setVelocityFromRotation(0.2,
            {
                0.0,
                angle + 60 * Mth:sin(self.getTimer() * 0.1) + userRot
            },
            true,
            true);
end

---@type THObjectContainer
local container = core.registerClass("testContainer")
function container:onInit()
    local userRot = self.getUser().getRotation().y
    self.getParameterManager().register("Double","userAngle",userRot)
    for i = 1,num  do
        local laser = self.createTHCurvedLaser(testLaser, {i}, self.getPosition(),1,12,0.5)
    end

    for i = 1,32 do
        ---@type THBullet
        local bullet = self.createTHBullet(testBullet, {i}, self.getPosition(),"arrow_big",1)
    end
end

---@param self THBullet
function container:onTick()
end