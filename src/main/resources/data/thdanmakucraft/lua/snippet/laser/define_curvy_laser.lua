---@label 定義彎曲雷射
---@icon laserbentcreate.png

---@type Class|THCurvedLaser
local curvyLaserClass = core.defineClass()

function curvyLaserClass:onInit()
    self:setWidth(0.08)
end
