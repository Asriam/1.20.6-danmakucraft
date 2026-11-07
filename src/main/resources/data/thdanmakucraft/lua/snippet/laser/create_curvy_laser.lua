---@label 建立彎曲雷射
---@icon lasercreate.png
---@description 建立彎曲雷射：顏色索引、長度、寬度；顏色索引用選單挑選。
---@parameter_switch colorIndex: 顏色索引 = {1, 2, 3, 4, 5, 6}
---@parameter length: 長度 = 40.0
---@parameter width: 寬度 = 0.08

local curvy = self:createTHCurvedLaser(curvyLaserClass, {}, self:getPosition(), ${colorIndex}, ${length}, ${width})