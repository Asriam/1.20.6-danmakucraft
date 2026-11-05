---@label 建立彎曲雷射
---@icon laserbentdefine.png
---@description 建立彎曲雷射：colorIndex 顏色索引、length 長度、width 寬度。
---@parameter colorIndex: 顏色索引 = 1
---@parameter length: 長度 = 40.0
---@parameter width: 寬度 = 0.08

local curvy = self:createTHCurvedLaser(curvyLaserClass, {}, self:getPosition(), ${colorIndex}, ${length}, ${width})
curvy:spawn()
