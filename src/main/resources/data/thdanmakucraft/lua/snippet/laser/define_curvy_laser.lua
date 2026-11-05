---@label 定義彎曲雷射
---@icon laserbentcreate.png
---@description 宣告一個彎曲雷射類別（THCurvedLaser），可設定寬度。
---@parameter width: 寬度 = 0.08

---@type Class|THCurvedLaser
local curvyLaserClass = core.defineClass()

function curvyLaserClass:onInit()
    self:setWidth(${width})
end