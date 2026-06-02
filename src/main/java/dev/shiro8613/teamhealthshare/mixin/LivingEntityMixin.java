package dev.shiro8613.teamhealthshare.mixin;

import dev.shiro8613.teamhealthshare.TeamHealthShare;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
Mixinファイル
MODにできてpluginにできないことの一つでこれがあるから何でもできる
Minecraftのプログラムを書き換えるために使用する

@Mixin(書き換えたいMinecraftのプログラム)

今回はLivingEntityクラス（マイクラの生きているエンティティの全てを定義するクラス）
 */

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    /*
    @Shadow Minecraft側で定義してある値を使いたいときに使う
    @Unique 追加してMinecraft側にはない値を使いたいときに使う
    @Inject Minecraft側に定義された関数（機能）に新しい機能を追加したいときに使う
    @At どこに機能を追加するのかを決める
        HEAD 前　この機能が呼ばれたら、Minecraft側で指定した機能が呼ばれる前にこちらで設定した機能を呼ぶ
        TAIL 後　この機能が呼ばれて、Minecraft側で指定した機能が呼ばれた後にこちらで設定した機能を呼ぶ
        その他いろいろ　どこが呼ばれたら～とかいろんな場所を指定して機能を追加することによって、Minecraftにカスタムの機能を作る
     */
    @Shadow
    private static EntityDataAccessor<Float> DATA_HEALTH_ID; //Minecraft側に定義された、HPのデータが格納されたID

    /*
    ほぼおまじない
    LivingEntityの機能を使いたいけど、このクラスはLivingEntityではないので変換してentityに保存する。
    (Object)とかやってるのは言語的な問題で、これを理解するのはレベルアップが必要
    この変換をキャストという
     */
    @Unique
    private final LivingEntity entity = (LivingEntity) (Object) this;

    //setHealthというHPを設定するところの機能が呼ばれる前に機能を追加
    @Inject(method = "setHealth", at = @At("HEAD"))
    //setHealthはHPをfloatで受け取る　floatはほぼ小数点が使える数字だと思っていい（厳密にはいろいろある）
    private void onSetHealth(float health, CallbackInfo ci) {
        // このentityがServerPlayer(プレーヤー） かつ（&&） プレーヤーが接続を完了して、動ける用になっていたら（connectionという物がnull(ない)状態だと、まだ接続処理でロード画面である事が判定できる）
        if (entity instanceof ServerPlayer player && player.connection != null) {
            /*
            MinecraftServerとは？　その名前の通りでMinecraftのServerである
            このMinecraftServerさんがプレーヤーの一覧などを管理しているので、プレーヤー取得のために必要
            今回はプレーヤーからlevel(ワールド)を取得して、そのワールドからサーバーを取得している
            これはよく使う取得方法
             */
            MinecraftServer server = player.level().getServer(); //MinecraftServer(サーバーを取得する)

            Team team = player.getTeam(); //プレーヤーが所属してるチームを取得
            //チームがnull(ない）状態は入っていない状態なので、処理を終了　もしくは　許可されたチームにそのチーム名が入っていない時も処理を終了
            // containsは含む、!を付けると結果を反転できる　→　結果を反転　入っている？　＝　入っていない
            if (team == null || !TeamHealthShare.ALLOW_TEAM.contains(team.getName())) {
                return; //処理を終了
            }

            PlayerList playerList = server.getPlayerList(); //サーバーからプレーヤー一覧を取得する
            for (String playerName : team.getPlayers()) { //ループ　チームのプレーヤー一覧を上から順番に取り出して、処理していく
                //チームのプレーヤー一覧にはプレーヤーの名前の文字列(String)しか入っていないので、サーバーのプレーヤーリストから名前で検索して、実際のプレーヤーを取り出す
                //チームのプレーヤーをターゲットと呼ぶ
                ServerPlayer target = playerList.getPlayer(playerName);
                // targetがnull(ない）＝存在しない
                // targetが死んでいるか、リスポーン待ちになっているとき
                // targetがプレーヤーと同じ ＝HPが変わったプレーヤーもチームに入っているから取り出せてしまう
                // targetとプレーヤーがほぼ同じ体力（Math.absは絶対値　正の数、負の数関係ない状態にする、今の体力と設定予定の体力を引いてその差が十分小さいかどうか）
                //上記4つのどれかに当てはまったら(||)処理をスキップ
                if(target == null || target.isDeadOrDying() || target.equals(player) || Math.abs(target.getHealth() - health) < 0.01f) {
                    continue;//処理をスキップ（ループを終了するわけではなく、次のプレーヤーに行く）
                }

                if (health <= 0) { //設定するHPが0以下ならtargetをkill！！
                    target.kill(target.level());//killを実行するには、ワールド(level)が必要なので、targetのワールドを取得して使う
                } else { //設定するHPが0以下ではない　＝設定しても死なない値だったら
                    /*
                    targetの設定を設定したいHP(health)にする
                    ここで、target.setHealthを呼びたくなるが、それを呼んでHPを設定すると、また同じ処理が流れて、無限ループしてしまう
                    対策のため、ここではsetHealthが内部で行っている処理を直接呼ぶことで、解決する

                    target.getEntityDataでそのプレーヤーが持っているデータを取り出す
                    これにsetを実行して値を入れると、自動でプレーヤーに同期してくれる！便利！！
                    setをするためには、どこに何を入れるかを指定する
                    今回はHPなので、DATA_HEALTH_IDを使う　
                    dataコマンドでいう,
                    /data modify entity @s Health set value 10f
                    的な事をしている　コマンドではプレーヤーデータは変更できないが、MODなら可能
                    ここのHealthがDATA_HEALTH_IDに相当している

                    HPは、0から最大HPまでではないといけないので、Math.clamp()を使って、その間に収めている
                    最大が20として
                    -1 なら 0, 21なら20のように、小さすぎたり、大きすぎる値を指定した範囲に収めてくれるがのMath.clamp

                    最大体力は人によって違うので、target.getMaxHealthで取得している
                     */

                    target.getEntityData()
                        .set(DATA_HEALTH_ID, Mth.clamp(health, 0.0F, target.getMaxHealth()));
                }
            }

        }
    }
}
