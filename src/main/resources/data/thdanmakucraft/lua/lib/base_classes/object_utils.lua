local util = {}

---@param target THObject
---@param vec3 math.Vec3
---@param setRot boolean
function util.setVelocity(target, vec3, setRot)
    target:setVelocity(vec3,setRot)
end
