---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2024/12/31 16:58
---

---@class AdditionalParameterManager
local M = {}

---@param type string "String","Integer","Float","Boolean","THObject"
function M:register(type, key, value) end

---@param key string
---@return string
function M:getString(key) end

---@param key string
---@return number:int
function M:getInteger(key) end

---@param key string
---@return number:float
function M:getFloat(key) end

---@param key string
---@return boolean
function M:getBoolean(key) end

---@param key string
---@return THObject
function M:getTHObject(key) end