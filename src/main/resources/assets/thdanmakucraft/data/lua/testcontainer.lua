---@type THObjectContainer
local container = {}

function container:onInit()
    for i = 1, 5 do
        local laser = self.createTHCurvedLaser(self.getPosition(),1,100,0.5)
        laser:setLifetime(140)
        laser:getParameterManager():register("Double","angle",360/5*i + 0.001)
        --laser:setBlend("mul_rev")
        self.getParameterManager():register("THObject","laser"..i,laser)
    end
end

function container:onTick()
    for i = 1, 5 do
        local laser = self.getParameterManager():getTHObject("laser"..i)
        if(core.isValid(laser)) then
            local angle = laser:getParameterManager():getDouble("angle")
            laser:setVelocityFromRotation(0.2,
                    newVec2(
                            0.0,
                            angle + 60 * Mth:sin(self.getTimer() * 0.1)
                    ),
                    true,
                    true);
        end

    end
end

return container