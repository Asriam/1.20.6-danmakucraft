---@label 刪除物件
---@icon unitdel.png
---@description 讓物件走死亡流程（會觸發碰撞／死亡事件後再移除）。
---@parameter target: 對象 = self

---@field self THBullet
${target}:setDead()
