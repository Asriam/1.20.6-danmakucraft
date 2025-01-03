---@type THObjectContainer
local container = {}
local num = 1

print("hyyyyyyyyyyyyyyyyyyy222")

function container:onInit()
    local userRot = self.getUser().getRotation().y
    self.getParameterManager().register("Double","userAngle",userRot)
    print("onInit fuckyou")
    --[[
    for i = 1,num  do
        local laser = self.createTHCurvedLaser(self.getPosition(),1,120,0.5)
        laser.setLifetime(600)
        laser.getParameterManager().register("Double","angle",360/num*i + 0.001)
        --laser:setBlend("mul_rev")
        self.getParameterManager().register("THObject","laser"..i,laser)
        laser.spawn()
    end]]

    for i = 1,12 do
        local bullet = self.createTHBullet(self.getPosition(),"arrow_big",1)
        bullet.setVelocity(core.newVec3(1.0,0.0,0.0):yRot(math.pi*2/12*i),true)
        bullet.spawn()
    end
end

function container:onTick()
    local userRot = self.getParameterManager().getDouble("userAngle")
    --[[
    for i = 1, num do
        ---@type THObject
        local laser = self.getParameterManager().getTHObject("laser"..i)
        if core.isValid(laser) then
            function laser:onTick()
                --print("sada")
                self.move(0,0.1,0)
            end

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
    ]]
end

testContainer = container

--return container