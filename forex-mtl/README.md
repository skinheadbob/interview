## Forex Proxy Exercise Notes

### Solution Design
Here is a recap of the requirements,

>The golden source OneFrame supports 1000 requests per day (we only have one token available); 
>
>The proxy needs to support at least 10,000 user requests per day;
>
>The proxy should return a rate not older than 5 minutes;

Let's use the term 'OneFrame-lookup' to refer to 'proxy query all rates from OneFrame'.

Intuitively, we would want a cache on proxy side to memorize latest OneFrame-lookup. 
If the proxy can 'eagerly' refresh the entire cache every 5 min, 
it could serve any number of user requests and only performs 24*60/5=288 OneFrame-lookup, well below the 1000 quota.

An alternative approach is to set a TTL (Time To Live) on the cache and 'lazily' perform OneFrame-lookup in case of cache-miss.

Pro&Con for 'eager' and 'lazy' approach,
* Eager
    * Pro
        * Predictable OneFrame-lookup behavior and stable SLA to proxy users
    * Con  
        * OneFrame quota will go to waste when there is no user requests
* Lazy
    * Pro
        * Will not waste OneFrame quota when no user requests for a long time (relative to TTL)
    * Con
        * Possible race condition after cache expires after TTL 
        e.g. cache miss for two different Pairs at the same time may cause proxy to perform two OneFrame-lookup

### Implementation Notes

This time I'm implementing the 'eager' approach to ensure the solution will not go over OneFrame quota.

The general idea is to periodically (every 2 min) perform OneFrame-lookup and cache all rates. 
When user requests arrive, return the rate from cache. 

* This is a [crude runnable version](https://github.com/skinheadbob/interview/blob/dev/forex-mtl/src/main/scala/forex/services/rates/interpreters/OneFrameLive.scala),
    * Using 'scalacache' as it could support remote cache such as Redis/Memcache for scalability.
    * Start a background Monix scheduler to poll OneFrame and update all rates in cache.
    * This version does NOT conform to the FP style as the rest of the project. The cache is populated inside OneFrame object.
    * I have created an Algebra-style [OneFrameClient](https://github.com/skinheadbob/interview/tree/dev/forex-mtl/src/main/scala/forex/services/oneframe), but haven't figured out how to use it to populate cache

* What's unfinished
    * Move self-refreshing cache logic in OneFrameLive from companion object to class (not sure if there is a proper FP approach)
    * Pass OneFrameLive config through 'ApplicationConfig' instead of hardcoding
    * Proper test cases, happy path and edge cases such as 'no such currency'