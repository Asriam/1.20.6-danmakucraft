---@label 建立雷射
---@icon lasercreate.png
---@description 從容器建立並生成一道直線雷射。
---@parameter colorIndex: 顏色索引 = 1
---@parameter length: 長度 = 3
---@parameter width: 半徑 = 1
---
local laser = self:createTHLaser(laserClass, {}, self:getPosition(),${colorIndex},${colorIndex},${length},${width})