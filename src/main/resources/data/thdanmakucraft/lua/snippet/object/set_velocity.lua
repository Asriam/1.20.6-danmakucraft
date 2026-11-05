---@label 設定速度
---@icon setv.png
---@description 設定物件每個 tick 的位移向量；setRotation 決定是否順便對齊朝向。
---@parameter x: X 分量 = 0.0
---@parameter y: Y 分量 = 0.0
---@parameter z: Z 分量 = 0.2
---@parameter_boolean setRotation: 對齊朝向 = true

object:setVelocity(math.vec3.new(${x}, ${y}, ${z}), ${setRotation})
