---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2025/1/1 00:23
---

---@class THLaser:THObject
local laser = {}

---@param index number
function laser:setLaserColorByIndex(index) end

---@param r number
---@param g number
---@param b number
---@param a number
function laser:setLaserColor(r,g,b,a) end

---@param width number
---@param duration number
function laser:growWidth(width, duration) end

---@param length number
---@param duration number
function laser:growLength(length, duration) end

---@param width number
function laser:setWidth(width) end

---@param length number
function laser:setLength(length) end

---@param width number
---@param length number
---@param duration number
function laser:grow(width, length, duration) end

---@param vec3 util.Vec3
---@param duration number
function laser:growVector(vec3, duration) end

---@param vec3 util.Vec3
function laser:vectorTo(vec3) end