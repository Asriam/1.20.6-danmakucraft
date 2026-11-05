---@label 設定重力
---@icon setgravity.png
---@description 用向下加速度模擬重力（apidoc 沒有 setGravity，這裡用 setAcceleration 代替）。
---@parameter gravity: 重力大小 = -0.05

-- 重力：向下持續加速
self:setAcceleration(math.vec3.new(0.0, ${gravity}, 0.0))
