---@type THObject
local object = {}

function object:onInit()

end

function object:onTick()
    self:setVelocityFromRotation(0.2,
            core.newVec2(
            360 / 16 + 60 * M.cos(self:getTimer() * 0.1),
            360 / 16 + 60 * M.sin(self:getTimer() * 0.1)
    ),
    true,
    true);
end

return object

