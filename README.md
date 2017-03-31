# cordova-plugin-pda
cordova-plugin-pda by woody 

date:2017-03-31

*******************************

1.The plugin only supports Android devices

2.Installation method：<pre><code>cordova plugin add https://github.com/hellowoody/cordova-plugin-pda.git</code></pre>

3.After installation, you need to modify the class "com.woody.plugins.pda.Util" in "import com.ionicframework.myplugins257081.R;" to “import com.ionicframework.XXXXXX.R;”

4.Add “Util.initSoundPool(this);” to the onCreate method in your project MainActivity.java， this code is designed to initialize the audio player

5.Use demo：

Identification M1 card (S50)

<pre><code>
woody.plugins.PdaPlugin.m1(function(data){
  alert(data);
},function(err){
  alert(err);
});
</code></pre>

Identification of CPU card (Fudan CPU)

<pre><code>
$scope.cpu = function(){
    woody.plugins.PdaPlugin.cpu(function(data){
      alert(data);
    },function(err){
      alert(err);
    });
}
</code></pre>

Identification of UHF tags (UHF)

<pre><code>
$scope.uhf = function(){
    woody.plugins.PdaPlugin.uhf(function(data){
      if(data != "")
      {
        alert(data);
      }
    },function(err){
      alert(err);
    });
}
$scope.stopuhf = function(){
    woody.plugins.PdaPlugin.stopuhf(function(data){
    },function(err){
      alert(err);
    });
}
</code></pre>

1.该插件只支持安卓设备

2.安装方法：<pre><code>cordova plugin add https://github.com/hellowoody/cordova-plugin-pda.git</code></pre>

3.安装后，需要将com.woody.plugins.pda.Util类中的"import com.ionicframework.myplugins257081.R;" 修改为自己的包名如“import com.ionicframework.XXXXXX.R";”

4.在你的项目MainActivity类中onCreate方法中增加Util.initSoundPool(this);这句代码，目的是为了初始化播放声音的工具类

5.使用演示：

识别m1卡（s50）

<pre><code>
woody.plugins.PdaPlugin.m1(function(data){
  alert(data);
},function(err){
  alert(err);
});
</code></pre>

识别cpu卡（复旦cpu）

<pre><code>
$scope.cpu = function(){
    woody.plugins.PdaPlugin.cpu(function(data){
      alert(data);
    },function(err){
      alert(err);
    });
}
</code></pre>

识别uhf标签（超高频功能）

<pre><code>
$scope.uhf = function(){
    woody.plugins.PdaPlugin.uhf(function(data){
      if(data != "")
      {
        alert(data);
      }
    },function(err){
      alert(err);
    });
}
$scope.stopuhf = function(){
    woody.plugins.PdaPlugin.stopuhf(function(data){
    },function(err){
      alert(err);
    });
}
</code></pre>
