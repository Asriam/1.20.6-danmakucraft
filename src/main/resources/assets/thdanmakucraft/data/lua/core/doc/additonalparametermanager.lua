---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2024/12/31 16:58
---

---@class Parameter
local P = {}

---@return any
function P:getValue() end

---@param value any
function P:setValue(value) end

------------------------------------------------------------------------------------------------
---
---@class AdditionalParameterManager
local M = {}

---@param type string "String","Integer","Float","Boolean","THObject"
---@param key string
---@param value any
function M.register(type, key, value) end

---@param key:string
---@param value any
function M.setValue(key, value) end

---@deprecated
---@param key string
---@return Parameter
function M.getParam(key) end

---@param key string
---@return any
function M.getValue(key) end

---@param key string
---@return string
function M.getString(key) end

---@param key string
---@return number:int
function M.getInteger(key) end

---@param key string
---@return number:float
function M.getFloat(key) end

---@param key string
---@return number:double
function M.getDouble(key) end

---@param key string
---@return boolean
function M.getBoolean(key) end

---@param key string
---@return THObject
function M.getTHObject(key) end

---@param key string
---@return THBullet
function M.getTHBullet(key) end

---@param key string
---@return THLaser
function M.getTHLaser(key) end

---@param key string
---@return THCurvedLaser
function M.getTHCurvedLaser(key) end
