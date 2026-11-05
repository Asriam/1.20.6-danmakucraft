---@label 建立子彈
---@icon bulletcreate.png

local bullet = self:createTHBullet(bulletClass, {}, self:getPosition(), "ball_mid", 1)
bullet:spawn()
