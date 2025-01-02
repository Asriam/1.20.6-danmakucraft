---@type THObjectContainer
local container = {}

local num = 1

function container:onInit()
    local userRot = self.getUser().getRotation().y
    self.getParameterManager().register("Double","userAngle",userRot)

    for i = 1,num  do
        local laser = self.createTHCurvedLaser(self.getPosition(),1,120,0.5)

        laser.onTick = function()

        end

        laser.setLifetime(140)
        laser.getParameterManager().register("Double","angle",360/num*i + 0.001)
        --laser:setBlend("mul_rev")
        self.getParameterManager().register("THObject","laser"..i,laser)
    end
end

function container:onTick()
    local userRot = self.getParameterManager().getDouble("userAngle")
    for i = 1, num do
        local laser = self.getParameterManager().getTHObject("laser"..i)
        if core.isValid(laser) then
            local angle = laser.getParameterManager().getDouble("angle")
            laser.setVelocityFromRotation(0.2,
                    {
                        x = 0.0,
                        y = angle + 60 * Mth:sin(self.getTimer() * 0.1) + userRot
                    },
                    true,
                    true);
        else
            self.discard()
        end

    end
end

return container