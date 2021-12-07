local keys = redis.call('lrange',KEYS[1],0,redis.call('llen',KEYS[1])) 
local result = {}
for key,value in pairs(keys) do
    local data = redis.call('get',value)
    if(data)
    then
        table.insert(result,data)
    else
        if(key == 1)
        then
            table.insert(result,1,value)
        else
            return nil
        end
    end
end
return result