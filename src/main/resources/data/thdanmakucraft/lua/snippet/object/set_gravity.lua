---@label 設定重力
---@icon setgravity.png
---@description 用向下加速度模擬重力（apidoc 沒有 setGravity，這裡用 setAcceleration 代替）。
---@parameter target: 對象 = self
---@parameter gravity: 重力大小 = 0.05
${target}:setGravity(${gravity})