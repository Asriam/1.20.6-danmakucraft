---@label 設定加速度
---@icon setaccel.png
---@description 設定物件每個 tick 的速度增量。
---@parameter x: X 分量 = 0.0
---@parameter y: Y 分量 = -0.01
---@parameter z: Z 分量 = 0.0

self:setAcceleration(math.vec3.new(${x}, ${y}, ${z}))
