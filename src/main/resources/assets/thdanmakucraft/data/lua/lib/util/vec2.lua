---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2025/1/23 下午 09:01
---

---@field x number
---@field y number
---@class util.Vec2:abstractVec2
local vec2 = {}

---@return util.Vec2
function vec2.new(x, y)
    local v = {
        x = x or 0,
        y = y or 0
    }
    setmetatable(v, {__index = vec2 })
    return v
end

util.vec2 = vec2.new

---@param _vec2 util.Vec2
---@return util.Vec2
function vec2:vectorTo(_vec2)
    return vec2.new(_vec2.x - self.x, _vec2.y - self.y)
end

---@param _vec2 util.Vec2
---@return util.Vec2
function vec2:normalize(_vec2)
    local length = self:length()
    return vec2.new(_vec2.x / length, _vec2.y / length)
end

---@param _vec2 util.Vec2
---@return util.Vec2
function vec2:dot(_vec2)
    return vec2.new(self.x * _vec2.x, self.y * _vec2.y)
end

---@param _vec2 util.Vec2
---@return util.Vec2
function vec2:subtract(_vec2)
    return vec2.new(self.x - _vec2.x, self.y - _vec2.y)
end

---@param _vec2 util.Vec2
---@return util.Vec2
function vec2:add(_vec2)
    return vec2.new(self.x + _vec2.x, self.y + _vec2.y)
end

---@param _vec2 util.Vec2
---@return util.Vec2
function vec2:distanceTo(_vec2)
    return math.sqrt((self.x - _vec2.x) ^ 2 + (self.y - _vec2.y) ^ 2)
end

---@param _vec2 util.Vec2
---@return util.Vec2
function vec2:distanceToSqr(_vec2)
    return (self.x - _vec2.x) ^ 2 + (self.y - _vec2.y) ^ 2
end

---@param _number number
---@return util.Vec2
function vec2:scale(_number)
    return vec2.new(self.x * _number, self.y * _number)
end

---@return util.Vec2
function vec2:reverse()
    return vec2.new(-self.x, -self.y)
end

---@param _vec2 util.Vec2
---@return util.Vec2
function vec2:multiply(_vec2)
    return vec2.new(self.x * _vec2.x, self.y * _vec2.y)
end

---@param _vec2 util.Vec2
---@return boolean
function vec2:equals(_vec2)
    return self.x == _vec2.x and self.y == _vec2.y
end

---@param _number number
---@return util.Vec2
function vec2:rotate(_number)
    local cos = math.cos(_number)
    local sin = math.sin(_number)
    return vec2.new(self.x*cos - self.y*sin, self.x*sin + self.y*cos)
end


---@return util.Vec2
function vec2:length()
    return math.sqrt(self.x ^ 2 + self.y ^ 2)
end

---@return util.Vec2
function vec2:lengthSquared()
    return self.x ^ 2 + self.y ^ 2
end