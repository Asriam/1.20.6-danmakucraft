---@label 建立子彈
---@icon bulletcreate.png
---@description 在容器位置建立並生成一顆子彈；style 是樣式名稱、colorIndex 是顏色索引。
---@parameter style: 子彈樣式名稱 = ball_mid
---@parameter colorIndex: 顏色索引 = 1

local bullet = self:createTHBullet(bulletClass, {}, self:getPosition(), ${style}, ${colorIndex})