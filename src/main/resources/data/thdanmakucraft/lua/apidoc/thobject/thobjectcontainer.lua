--- DateTime: 2024/12/31 01:08

---@class THObjectContainer
---@field onInit fun(self)
---@field onTick fun(self)
---@field taskManager TaskManager
---@field timer number read only
---@field autosave AutoSaveData
local container = {}

---@return number
function container:getMaxObjectAmount() end

---@return util.Vec3
function container:getPosition() end

--[[
---@param time number
function container:setTimer(time) end
]]

---@return number
function container:getTimer() end

---@return Entity
function container:getUser() end

---@return Entity
function container:getTarget() end

function container:clearObjects() end

---@param class Class
---@param args table
---@param position util.Vec3
---@return THObject
function container:createTHObject(class, args, position) end

---@param class Class
---@param args table
---@param position util.Vec3
---@param style string
---@param colorIndex number
---@return THBullet
function container:createTHBullet(class, args, position, style, colorIndex) end

---function container:createTHLaser() end

---@param class Class
---@param args table
---@param position util.Vec3
---@param colorIndex number
---@param length number
---@param width number
---@return THCurvedLaser
function container:createTHCurvedLaser(class, args, position, colorIndex, length, width) end

---@param class Class
---@param args table
---@param position util.Vec3
---@param colorIndex number
---@param length number
---@param width number
---@return THLaser
function container:createTHLaser(class, args) end

--[[
---@deprecated
---@return AdditionalParameterManager
function container:getParameterManager() end
]]

function container:discard() end

---@param class Class
---@param args table
---@param position util.Vec3
---@return THObject
function container:newTHObject(class, args) end

---@param cardName string
function container:setSpellCardName(cardName) end

---@param lifetime number
function container:setLifetime(lifetime) end

---@param amount number
function container:getMaxObjectAmount(amount) end

THObjectContainer = container