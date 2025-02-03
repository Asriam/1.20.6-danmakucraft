---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2025/1/9 23:35
---

---@class Task
---@field progress number
---@field co thread
---@field runnable fun(target:table)
task = {}

---@param target table
---@param runnable fun(target:table)
---@return Task
function task.new(runnable)
    ---@type Task
    local t = {
        co = coroutine.create(runnable),
        progress = 0
    }
    return t
end

---@param time number
function task.wait(time)
    for i = 1, (time or 1) do
        coroutine.yield()
    end
end