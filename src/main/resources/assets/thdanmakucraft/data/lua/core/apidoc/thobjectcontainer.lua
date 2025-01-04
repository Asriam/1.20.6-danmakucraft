---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2024/12/31 01:08
---

---@class THObjectContainer
---@field onInit fun(self)
---@field onTick fun(self)
local container = {
}

---@return number
function container.getMaxObjectAmount() end

---@return Vec3
function container.getPosition() end

---@param time number
function container.setTimer(time) end

---@return number
function container.getTimer() end

---@return Entity
function container.getUser() end

---@return Entity
function container.getTarget() end

function container.clearObjects() end

---@param key string
---@param position Vec3
---@return THObject
function container.createTHObject(key, position) end

---@param key string
---@param position Vec3
---@param style string
---@param colorIndex number
---@return THBullet
function container.createTHBullet(key, position,style,colorIndex) end

---function container:createTHLaser() end

---@param key string
---@param position Vec3
---@param colorIndex number
---@param length number
---@param width number
---@return THCurvedLaser
function container.createTHCurvedLaser(key, position,colorIndex,length,width) end

---@return AdditionalParameterManager
function container.getParameterManager() end

function container.discard() end