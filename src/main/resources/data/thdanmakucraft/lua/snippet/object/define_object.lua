---@label 定義物件
---@icon objectdefine.png
---@description 宣告一個一般物件類別（THObject），可自行在 onInit / onTick 加入行為。

---@type Class|THObject
local objectClass = core.defineClass()

function objectClass:onInit()
end

function objectClass:onTick()
end