# チームごと、体力同期MOD

注意：　
 - バグがあるかもしれないです
 - HPのみの共有で、最大HPや、金リンゴを食べたときの付与効果とうは同期されません

MODって英語だけで、何かいてるかわからんって人が、"大体どんなことをやってるのかな～" 程度でも良いから理解できるようにコメントが入っています

肝心のプログラム自体は、`src/main/java/dev/shiro8613/teamhealthshare`の中にあります

`TeamHealthShare.java`がMODの最初に読み込まれるファイルです

`mixin/LivingEntityMixin`がMixin（Minecraftのプログラムをいじくるやつ）です


何やってるかとかを理解するサンプルみたいな物なので、軽めに実装して、関数化などはしていません

そもそもここまでの環境構築や、Mixinのやり方などがわからないと思うので、それはMODを勉強しよう！ってときに覚えましょう

このコードを呼んで雰囲気だけでも理解してもらえると嬉しいです

一応[リリース](https://github.com/shiro8613/TeamHealthShare/releases)にサンプルで作ってビルドしたものが入っています

Minecraft Fabric 26.2
サーバー側のみ導入、FabricAPIなし