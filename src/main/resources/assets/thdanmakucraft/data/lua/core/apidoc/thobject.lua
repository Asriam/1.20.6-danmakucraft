---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2024/12/31 00:36
---
---@class THObject:table
---@field onInit   fun(self)
---@field onTick   fun(self)
---@field onHit    fun(self)
---@field onDead   fun(self)
---@field onRemove fun(self)
---@field x number
---@field y number
---@field z number
---@field type string
---@field uuid string
local object = {
}

---@param x number
---@param y number
---@param z number
---@overload fun(vec3:Vec3)
function object.setPosition(x,y,z) end

---@param life number
function object.setLifetime(life) end

---@param x number
---@param y number
---@param z number
---@overload fun(vec3:Vec3)
function object.setScale(x,y,z) end

---@param x number
---@param y number
---@param z number
---@overload fun(vec3:Vec3)
function object.setSize(x,y,z) end

---@param velocity Vec3
---@param shouldSetRotation boolean
---@overload fun(velocity:table|{x,y,z}, setRotation:boolean)
function object.setVelocity(velocity, setRotation) end

---@param speed number
---@param direction Vec3
---@param setRotation boolean
---@overload fun(speed:number, direction:table|{x,y,z}, setRotation:boolean)
function object.setVelocityFromDirection(speed, direction, setRotation) end

---@param speed number
---@param rotation Vec2
---@param isDeg boolean
---@param setRotation boolean
---@overload fun(speed:number, rotation:table|{x,y}, isDeg:boolean, setRotation:boolean)
function object.setVelocityFromRotation(speed, rotation, isDeg, setRotation) end

---@param acceleration Vec3
function object:setAcceleration(acceleration) end

---@param acceleration number
---@param direction Vec3
---@overload fun(acceleration:number, direction:table|{x,y,z})
function object.setAccelerationFromDirection(acceleration, direction) end

---@param acceleration number
---@param rotation Vec3
---@param isDeg boolean
------@overload fun(acceleration:number, direction:table|{x,y}, isDeg:boolean)
function object.setAccelerationFromRotation(acceleration, rotation, isDeg) end

---@param x number
---@param y number
---@param z number
function object.setRotation(x,y,z) end

---@param vec3 Vec3
---@overload fun(vec3:table|{x,y,z})
function object.setRotationByDirectionalVector(vec3)  end

---@param r number
---@param g number
---@param b number
---@param a number
function object.setColor(r,g,b,a)  end

---@param blend string enum values:"normal","add","mul_rev","mul+add","max","min"
function object.setBlend(blend) end

---@param type number
---@overload fun(type:string)
function object.setCollisionType(type) end

---@return number
function object.getTimer()  end

---@return THObjectContainer
function object.getContainer() end

---@return Vec3
function object.getPosition()  end

---@return Vec3
function object.getPrePosition()  end

---@return number
function object.getSpeed()  end

---@return Vec3
function object.getVelocity()  end

---@return Vec3
function object.getMotionDirection()  end

---@return Vector3f
function object.getRotation()  end

---@deprecated
---@return number
function object.getXRot()   end

---@deprecated
---@return number
function object.getYRot()   end

---@deprecated
---@return number
function object.getZRot()   end

---@return Vec3
function object.getAcceleration()  end

---@return Vector3f
function object.getScale() end

---@return Vec3
function object.getSize() end

---@return string
function object.getBlendName() end

---@return AdditionalParameterManager
function object.getParameterManager() end

---@param x number
---@param y number
---@param z number
---@overload fun(vec3:Vec3)
function object.move(x,y,z) end

function object.setDead()  end

---@param flag boolean
function object.setShouldSetDeadWhenCollision(flag) end

function object.remove()  end

function object.spawn() end

---@return boolean
function object.isSpawned() end

---@param flag boolean
function object.setNavi(flag) end

--[[
---@deprecated
function object:override() end

---@deprecated
---@return number
function object.getX()  end

---@deprecated
---@return number
function object.getY()  end

---@deprecated
---@return number
function object.getZ()  end
]]