---@label 定義雷射
---@icon laserdefine.png

---@type Class|THLaser
local laserClass = core.defineClass()

function laserClass:onInit()
    self:setWidth(0.1)
    self:setLength(40.0)
end
