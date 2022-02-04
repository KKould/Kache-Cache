local keys = redis.call('lrange',KEYS[1],0,redis.call('llen',KEYS[1]))
local keySize = table.getn(keys)  
if(next(keys) == nil)  
then
    return nil
elseif(keySize == 1)
then
    return keys
end
local keyFirst = keys[1]
table.remove(keys,1)
local result = redis.call('mget',unpack(keys))
table.insert(result,1,keyFirst)
if(keySize == table.getn(result))
then
    return result
else
    return nil
end