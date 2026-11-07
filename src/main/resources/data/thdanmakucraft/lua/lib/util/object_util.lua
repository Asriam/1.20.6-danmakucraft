local util = {}

---@param target THObject
---@param vec3 math.Vec3
---@param setRot boolean
function util.setVelocity(target, vec3, setRot)
    target:setVelocity(vec3,setRot)
end

---@param target THObject
---@param vec3 math.Vec3
function util.setAcceleration(target, vec3)
    target:setAcceleration(vec3)
end