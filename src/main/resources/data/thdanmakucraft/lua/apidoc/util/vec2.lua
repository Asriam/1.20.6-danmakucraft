---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2024/12/7 上午 05:01
---

---@class core.Vec2:abstractVec2
---@field x number
---@field y number
local Vec2 = {}

---@param number number
---@return core.Vec2
function Vec2:scale(number) end

---@param vec2 core.Vec2
---@return core.Vec2
function Vec2:dot(vec2) end

---@overload fun(number:number)
---@param vec2 core.Vec2
---@return core.Vec2
function Vec2:add(vec2) end

---@param vec2 core.Vec2
---@return boolean
function Vec2:equals(vec2) end

---@return core.Vec2
function Vec2:normalized() end

---@return number
function Vec2:length() end

---@return number
function Vec2:lengthSquared() end

---@param vec2 core.Vec2
---@return number
function Vec2:distanceToSqr(vec2) end

---@return core.Vec2
function Vec2:negated() end
