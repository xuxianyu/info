package com.ming.api.service;

import com.ming.entity.DataDict;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.ObservableExecutionMode;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheKey;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheRemove;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheResult;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rx.Observable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 自定义实现 command 方式访问远程接口
 *
 * @author ming
 * @date 2017-10-30 11:23
 */
@Service
@Deprecated
public class DictServiceX {
    @Autowired
    private RestTemplate restTemplate;

    //服务降级 顶级服务---》次级服务 v2--------》最低级别v1  是调用端 运行时间来做熔断
    //设定 这个服务使用熔断机制  熔断回调方法 是v2  忽略runtimeexception

  /*  *//**
     * 注解实现 同步访问
     *
     * @author ming
     * @date 2017-10-09 15:40
     *//*
    @CacheResult(cacheKeyMethod = "cacheKey")
    @HystrixCommand(fallbackMethod = "v2", ignoreExceptions = RuntimeException.class)
    public String dictAll() throws InterruptedException {
        return restTemplate.getForObject("http://COMMON-SERVICE/dict/all?username=ming", String.class);
    }
    */

    /**
     * CacheResult 的cacheKeyMethod 指定key的方法
     *
     * @author ming
     * @date 2017-10-10 13:54
     *//*
    private String cacheKey(String str){
        return str;
    }*/
    @CacheResult
    @HystrixCommand(fallbackMethod = "v2")
    public DataDict findDictById(Long id) throws InterruptedException {
        Thread.sleep(30000L);
        return restTemplate.getForObject("http://COMMON-SERVICE/dict/" + id, DataDict.class);
    }

    @CacheResult
    @HystrixCommand(fallbackMethod = "v2")
    public List findDictListByIds(Collection<Long> ids) {
        return restTemplate.getForObject("http://COMMON-SERVICE/dict/" + ids, List.class);
    }

    /**
     * 注解实现 同步访问
     *
     * @author ming
     * @date 2017-10-09 15:40
     */
    @CacheResult
    @HystrixCommand(fallbackMethod = "v2", ignoreExceptions = RuntimeException.class)
    public String dictAll() throws InterruptedException {
        return restTemplate.getForObject("http://COMMON-SERVICE/dict/all?username=ming", String.class);
    }

    /**
     * 更新的时候 清除请求缓存
     *
     * @author ming
     * @date 2017-10-10 14:01
     */
    @CacheRemove(commandKey = "dictAll")
    @HystrixCommand
    public void update(@CacheKey("str") String str) {
        restTemplate.getForObject("http://COMMON-SERVICE/dict/update?username=ming", String.class);
    }

    /**
     * 通过 @HystrixCommand 实现异步访问
     *
     * @author ming
     * @date 2017-10-09 15:39
     */
    @HystrixCommand(fallbackMethod = "v2", ignoreExceptions = RuntimeException.class)
    public Future<String> asyncDictAll() {
        return new AsyncResult<String>() {
            @Override
            public String invoke() {
                return restTemplate.getForObject("http://COMMON-SERVICE/dict/all?username=ming", String.class);
            }
        };
    }

    /**
     * Observable 模式实现 通过 observableExecutionMode指定是
     * 默认是observe{@linkplain ObservableExecutionMode#EAGER}
     * toObservable {@linkplain ObservableExecutionMode#LAZY}
     *
     * @author ming
     * @date 2017-10-09 16:12
     */
    @HystrixCommand(fallbackMethod = "v2"
            , observableExecutionMode = ObservableExecutionMode.LAZY
            , commandKey = "commandKey"
            , groupKey = "groupKey"
            , threadPoolKey = "threadPoolKey"
    )
    public Observable<String> observableDictAll() {
        return Observable.create(subscriber -> {
            try {
                if (subscriber.isUnsubscribed()) {
                    String str = restTemplate.getForObject("http://COMMON-SERVICE/dict/all?username=", String.class);
                    subscriber.onNext(str);
                    subscriber.onCompleted();
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }
/*

    */
/**
 * 通过自定义command 实现同步调用
 *
 * @author ming
 * @date 2017-10-09 15:38
 *//*

    public String dictByCommandExecute() {
        return new DictCommand(restTemplate, "ming").execute();
    }

*/

   /* *//**
     * 通过自定义command 实现异步调用
     *
     * @author ming
     * @date 2017-10-09 15:39
     *//*
    public String dictByCommandQueue() throws ExecutionException, InterruptedException {
        return new DictCommand(restTemplate, "ming").queue().get();
    }


    *//**
     * 通过自定义 observableCommand 响应式
     *
     * @author ming
     * @date 2017-10-09 16:05
     *//*
    public Observable<String> dictByObservableCommandObserve() {
        return new DictObservableCommand(restTemplate).observe();
    }

    */

    /**
     * 通过自定义 observableCommand 响应式
     *
     * @author ming
     * @date 2017-10-09 16:05
     */
    public void v2(Long obj, Throwable e) throws InterruptedException {
        //降级
        //Thread.sleep(30000);
        //return "v2级别 降级" + e.getMessage();
    }
    /*
    public Observable<String> dictByObservableCommandToObservable() {
        return new DictObservableCommand(restTemplate).toObservable();
    }*/

    public void v1(Throwable e) {
        //  return "v1级别 最终级别 熔断" + e.getMessage();
    }
}
