---@label 定義子彈
---@icon bulletdefine.png

---@type Class|THBullet
local bulletClass = core.defineClass()

function bulletClass:onInit()
    self:setVelocity(math.vec3.new(0.0, 0.0, 0.2), true)
end

function bulletClass:onTick()
end
