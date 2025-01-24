---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2024/12/31 01:08
---

---@class THObjectContainer
---@field onInit fun(self)
---@field onTick fun(self)
---@field source userdata
---@field parameterManager AdditionalParameterManager
---@field params table auto save parameters
local container = {}

---@return number
function container:getMaxObjectAmount() end

---@return core.Vec3
function container:getPosition() end

---@param time number
function container:setTimer(time) end

---@return number
function container:getTimer() end

---@return Entity
function container:getUser() end

---@return Entity
function container:getTarget() end

function container:clearObjects() end

---@param key string
---@param args table
---@param position core.Vec3
---@return THObject
function container:createTHObject(key,args,position) end

---@param key string
---@param args table
---@param position core.Vec3
---@param style string
---@param colorIndex number
---@return THBullet
function container:createTHBullet(key,args,position,style,colorIndex) end

---function container:createTHLaser() end

---@param key string
---@param args table
---@param position core.Vec3
---@param colorIndex number
---@param length number
---@param width number
---@return THCurvedLaser
function container:createTHCurvedLaser(key,args,position,colorIndex,length,width) end

---@return AdditionalParameterManager
function container:getParameterManager() end

function container:discard() end

THObjectContainer = container