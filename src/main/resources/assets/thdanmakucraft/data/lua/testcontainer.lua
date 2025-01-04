---@type Class
local testBullet = core.registerClass("testBullet")
---@param self THBullet
function testBullet.onInit(self)

end

---@param self THBullet
function testBullet.onTick(self)
    self.move(0.0,0.1,0.0)
end

local testBullet2 = core.registerClass("testBullet2", testBullet)

---@param self THBullet
function testBullet2.onInit(self)
end

---@param self THBullet
function testBullet2.onTick(self)
    testBullet2.super.onTick(self)
end

---@type Class
local container = core.registerClass("testContainer")
local num = 1

---@param self THBullet
function container.onInit(self)
    local userRot = self.getUser().getRotation().y
    self.getParameterManager().register("Double","userAngle",userRot)
    for i = 1,num  do
        local laser = self.createTHCurvedLaser(testBullet2, self.getPosition(),1,120,0.5)
        laser.setLifetime(600)
        laser.getParameterManager().register("Double","angle",360/num*i + 0.001)
        --laser:setBlend("mul_rev")
        self.getParameterManager().register("THObject","laser"..i,laser)
        laser.spawn()
    end

    for i = 1,12 do
        local bullet = self.createTHBullet(testBullet2, self.getPosition(),"arrow_big",1)
        bullet.setVelocity(core.newVec3(0.2,0.0,0.0):yRot(math.pi*2/12*i),true)
        bullet.spawn()
    end
end

---@param self THBullet
function container.onTick(self)
    local userRot = self.getParameterManager().getDouble("userAngle")
    for i = 1, num do
        ---@type THObject
        local laser = self.getParameterManager().getTHObject("laser"..i)
        if core.isValid(laser) then
            local angle = laser.getParameterManager().getDouble("angle")
            laser.setVelocityFromRotation(0.2,
                    {
                        0.0,
                        angle + 60 * Mth:sin(self.getTimer() * 0.1) + userRot
                    },
                    true,
                    true);
        else
            self.discard()
        end
    end
end