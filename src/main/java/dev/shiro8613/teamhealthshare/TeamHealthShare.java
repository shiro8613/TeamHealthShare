package dev.shiro8613.teamhealthshare;
/*
importはここじゃないところに書いたプログラムを使いたいときに書く。
最初のうちはほぼおまじない
publicになっていないと読み込めない
 */

import java.util.List;
import net.fabricmc.api.ModInitializer;

/*
ほぼおまじない
publicで外部からも使える物である
classはjavaでは最小単位みたいな物で、機能の集合
implementsは他で定められた型枠を適応するためのもの　鍵穴にあう鍵を作ってあげるかんじ
 */
public class TeamHealthShare implements ModInitializer {

    /*
    共有したいチーム
    特定のチームでのみ使えるように制限する用

    どこからでも使えるように public static
    一度初期化したら変更できなくする final
    文字列がいつかある集合である List<String>
    Listが集合、Stringが文字列

    List.of()　中身を入れて初期化する
    */
    public static final List<String> ALLOW_TEAM = List.of("red", "blue");

    @Override
    public void onInitialize() {
        //本来は最初にMODが読み込まれた時の処理を書く
        // 設定ファイルの読み込みとか
    }
}
