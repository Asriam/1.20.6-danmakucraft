---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2024/12/31 00:36
---
---@class THObject
---@field onInit   fun(self)
---@field onTick   fun(self)
---@field onHit    fun(self)
---@field onDead   fun(self)
---@field onRemove fun(self)
local object = {
}

---@param x number
---@param y number
---@param z number
---@overload fun(vec3:Vec3)
function object:setPosition(x,y,z) end

---@param x number
---@param y number
---@param z number
---@overload fun(vec3:Vec3)
function object:setScale(x,y,z) end

---@param x number
---@param y number
---@param z number
---@overload fun(vec3:Vec3)
function object:setSize(x,y,z) end

---@param velocity Vec3
---@param shouldSetRotation boolean
function object:setVelocity(velocity, shouldSetRotation) end

---@param speed number
---@param direction Vec3
---@param setRotation boolean
function object:setVelocityFromDirection(speed, direction, setRotation) end

---@param speed number
---@param rotation Vec2
---@param isDeg boolean
---@param setRotation boolean
function object:setVelocityFromRotation(speed, rotation, isDeg, setRotation) end

---@param acceleration Vec3
function object:setAcceleration(acceleration) end

---@param acceleration number
---@param direction Vec3
function object:setAccelerationFromDirection(acceleration, direction) end

---@param acceleration number
---@param rotation Vec3
---@param isDeg boolean
function object:setAccelerationFromRotation(acceleration, rotation, isDeg) end

---@param x number
---@param y number
---@param z number
---@overload fun(rotation:Vec3)
---@overload fun(rotation:Vec2)
function object:setRotation(x,y,z) end

---@param vec3 Vec3
function object:setRotationByDirectionalVector(vec3)  end

---@param r number
---@param g number
---@param b number
---@param a number
function object:setColor(r,g,b,a)  end

---@param blend string enum values:"normal","add","mul_rev","mul+add","max","min"
function object:setBlend(blend) end

---@param type number
---@overload fun(type:string)
function object:setCollisionType(type) end

---@return number
function object:getTimer()  end

---@return THObjectContainer
function object:getContainer() end

---@return Vec3
function object:getPosition()  end

---@return number
function object:getX()  end

---@return number
function object:getY()  end

---@return number
function object:getZ()  end

---@return Vec3
function object:getPrePosition()  end

---@return number
function object:getSpeed()  end

---@return Vec3
function object:getVelocity()  end

---@return Vec3
function object:getMotionDirection()  end

---@return Vector3f
function object:getRotation()  end

---@return number
function object:getXRot()   end

---@return number
function object:getYRot()   end

---@return number
function object:getZRot()   end

---@return Vec3
function object:getAcceleration()  end

---@return Vector3f
function object:getScale() end

---@return Vec3
function object:getSize() end

---@return string
function object:getBlendName() end

---@param x number
---@param y number
---@param z number
---@overload fun(vec3:Vec3)
function object:move(x,y,z) end

function object:setDead()  end

function object:remove()  end

THObjectClass = object