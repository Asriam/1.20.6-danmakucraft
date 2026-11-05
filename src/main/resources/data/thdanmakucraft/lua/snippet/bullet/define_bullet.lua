---@label 定義子彈
---@icon bulletdefine.png
---@description 宣告一個子彈類別（THBullet），onInit 設定初速、onTick 每 tick 更新。

---@type Class|THBullet
local bulletClass = core.defineClass()

function bulletClass:onInit()
    self:setVelocity(math.vec3.new(0.0, 0.0, 0.2), true)
end

function bulletClass:onTick()
end
