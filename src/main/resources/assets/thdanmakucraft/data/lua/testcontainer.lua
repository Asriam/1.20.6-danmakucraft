---@type THObjectContainer
local container = {}

function container:onInit()

end

function container:onTick()
    local laser = self:createTHCurvedLaser(self:getPosition(),1,10,0.5)

    laser:setVelocityFromRotation(0.2,
            newVec2(
                    360 / 16 + 60 * Mth:cos(self:getTimer() * 0.1),
                    360 / 16 + 60 * Mth:sin(self:getTimer() * 0.1)
            ),
            true,
            true);
end

return container