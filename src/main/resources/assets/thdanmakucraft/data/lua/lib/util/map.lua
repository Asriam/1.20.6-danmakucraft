---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2025/1/24 下午 05:06
---

---@class Map
local m = {}
map = m

---@return Map
function m.new()
    local m = {}
    setmetatable(m, {__index = m })
    return
end

function m:put(key, value)
    self[key] = value
end

function m:get(key)
    return self[key]
end

function m:remove(key)
    self[key] = nil
end

function m:clear()
    for k, v in pairs(self) do
        self[k] = nil
    end
end

function m:size()
    return #self
end

function m:containsKey(key)
    return self[key] ~= nil
end

function m:containsValue(value)
    for k, v in pairs(self) do
        if v == value then
            return true
        end
    end
    return false
end

---@param func fun(key, value)
function m:forEach(func)
    for k, v in pairs(self) do
        func(k, v)
    end
end