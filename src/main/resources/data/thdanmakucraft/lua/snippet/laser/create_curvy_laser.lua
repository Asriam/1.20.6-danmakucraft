---@label 建立彎曲雷射
---@icon laserbentdefine.png

local curvy = self:createTHCurvedLaser(curvyLaserClass, {}, self:getPosition(), 1, 40.0, 0.08)
curvy:spawn()
