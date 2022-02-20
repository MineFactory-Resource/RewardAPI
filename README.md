# **RewardAPI**

[![Tested-Version](https://img.shields.io/badge/tested%20mc%20version-1.18.1-blue.svg?style=flat-square)](https://papermc.io/downloads#Paper-1.18)
[![Release](https://img.shields.io/github/v/release/MineFactory-Resource/RewardAPI.svg?display_name=release&style=flat-square)](https://github.com/MineFactory-Resource/RewardAPI/releases)
[![Release-Date](https://img.shields.io/github/release-date/MineFactory-Resource/RewardAPI.svg?display_name=release&style=flat-square)](https://github.com/MineFactory-Resource/RewardAPI/releases)

**RewardAPI**는 대다수의 보상을 지급하는 시스템에서 사용할 수 있는 API입니다.  
모든 아이템 형태의 보상을 지급받기 위해서는 사용자의 인벤토리의 남은 공간을 확인해야 합니다.  
또 유저가 온라인이 아닐 때도 아이템을 지급해야 한다면 상당히 난처할 것입니다.  
이를 위해서 플러그인 따로따로 보상 지급을 구현하는 것이 아닌, 서버 차원에서 보상 지급을 통일해보세요!

## 개발 및 테스트 환경

[Paper 1.18.1](https://papermc.io/downloads#Paper-1.18)

## 플러그인 설치 방법

1. [Release](https://github.com/MineFactory-Resource/RewardAPI/releases) 에서 최신 버전의 플러그인 파일을 다운로드
2. 서버 폴더 중 plugins 폴더에 다운로드 받은 파일을 넣기

## Config 설정

### Command

커맨드의 별칭을 지정할 수 있습니다.

```yaml
  Command:
    aliases: [reward, storagebox, 보관함, 리워드]
```

---
### Menu

보관함의 GUI를 커스텀 할 수 있습니다.

```yaml
StorageBox:
  title: '보관함'
  rows: 6
  Pattern:
    - '_________'
    - '_________'
    - '_________'
    - '_________'
    - '_________'
    - 'LAAAAAAAR'
  Items:
    A:
      type: 'orange_stained_glass_pane'
      name: ''
```

`title` 과 `row`는 각각 GUI의 이름과 줄 개수를 지정합니다.
보관함은 기본적으로 패턴 형식으로 아이템을 설정합니다.
`Pattern:` 목록의 한 글자는 GUI 안의 슬롯 한 칸을 의미합니다.
이때 `'_'`는 보상을, `' '`(스페이스바)는 아무것도 지정하지 않습니다.
`'L'`과 `'R'`은 각각 페이지를 왼쪽, 오른쪽으로 넘기는 버튼입니다.
이외에 다른 글자는 `Items:`에서 아이템을 지정해야 합니다.

#### 예제

예를 들어서 아래처럼 설정한다면, 이름이 `'보관함'`이고 2줄이며,
첫 번째 줄은 양옆에 왼쪽과 오른쪽으로 넘기는 버튼과 주황색 색유리,
정중앙에는 안내 표지판이 있을 것이고, 두 번째 줄은 4칸의 보상이 있을 것입니다.

```yaml
StorageBox:
  title: '보관함'
  rows: 2
  Pattern:
    - 'LAAABAAAR'
    - ' _ _ _ _ '
  Items:
    A:
      type: 'orange_stained_glass_pane'
      name: ''
    B:
      type: 'oak_sign'
      name: '&a보상을 클릭하여 수령하세요!'
```



#### Buttons

왼쪽, 오른쪽으로 페이지를 넘기는 버튼은 `Buttons:` 에서 설정할 수 있습니다.
`Can:` 은 페이지 넘기기가 가능할 때, `Cant`는 페이지 넘기기가 불가능할 때 표시할 아이템을 지정합니다.
```yaml
  Buttons:
    Left:
      Can:
        type: 'player_head'
        name: '&6왼쪽으로 넘기기'
        head-texture: 'f7aacad193e2226971ed95302dba433438be4644fbab5ebf818054061667fbe2'
      Cant:
        type: 'orange_stained_glass_pane'
        name: ''
    Right:
      Can:
        type: 'player_head'
        name: '&6오른쪽으로 넘기기'
        head-texture: 'd34ef0638537222b20f480694dadc0f85fbe0759d581aa7fcdf2e43139377158'
      Cant:
        type: 'orange_stained_glass_pane'
        name: ''
```

#### Item 설정
`Items:`와 `Buttons`에서 사용할 수 있는 아이템 포맷입니다.

```yaml
type: 'DIAMOND_SWORD'
```

아이템의 종류를 지정합니다.

```yaml
name: '&a전설의 &b다이아몬드 검'
```

아이템의 이름을 지정합니다.

```yaml
lore:
  - ''
  - '&6옛 구전에서만 나오던 검입니다.'
```

아이템의 로어를 지정합니다.

```yaml
head-name: 'MHF_ArrowLeft'
```
머리 스킨의 주인을 닉네임으로 지정합니다. type이 플레이어 머리일때만 사용 가능합니다.

```yaml
head-texture: 'f7aacad193e2226971ed95302dba433438be4644fbab5ebf818054061667fbe2'
```
머리 스킨을 지정합니다. type이 플레이어 머리일때만 사용 가능합니다.
https://textures.minecraft.net/texture/ 뒤에 오는 문자열입니다.

---
### Message

```yaml
Message:
  Menu:
    out_of_space: '&c공간이 부족합니다.'
```

아이템 보상을 수령받을때 인벤토리에 공간이 부족하다면 뜨는 메시지를 설정할 수 있습니다.

---
### Database

데이터 저장과 관련된 설정을 할 수 있습니다.

```yaml
Database:
  save-interval: 300
  type: Json
  Json:
    datafolder: './plugins/RewardAPI/data'
  MySQL:
    host: localhost
    port: 3306
    database: minecraft
    tablename: rewardapi_rewards
    parameters: '?characterEncoding=utf8'
    username: root
    password: password
```

`save-interval: 300` 은 데이터 저장 주기를 초단위로 지정합니다.  
`type: Json` 은 데이터 저장 방식을 지정합니다. `Json`과 `MySQL`을 사용할 수 있습니다.

## Command

- `/reward` - 보관함을 엽니다.
  + 권한: `rewardapi.user`
- `/reward 주기 [플레이어] [보일아이템] [커맨드]` - 해당 플레이어에게 보상을 지급합니다.
  + 권한: `rewardapi.admin`
  + `[보일아이템]`은 `아이템_ID{데이터}` 형태로 사용해야 합니다.
    * 데이터는 마인크래프트 바닐라 give 커맨드의 data tag와 형식이 같습니다.
  + 또는 `'hand'`를 입력해 손에 들고 있는 아이템을 보이게 할 수도 있습니다.
  + `[커맨드]`에는 %p 가 플레이어 이름으로 치환됩니다.
  + 예시:
    * `/reward 주기 Uni_Lesser diamond_axe{display:{Name:'[{"text":"§a전설의 도끼"}]'}} 보상 지급 %p 전설의도끼`
    * 보상 수령시 `보상 지급 Uni_Lesser 전설의도끼` 커맨드가 실행됩니다.

## Java API Example
```java
import java.util.UUID;
import net.teamuni.rewardapi.api.StorageBoxAPI;
import net.teamuni.rewardapi.data.object.ItemReward;
import net.teamuni.rewardapi.data.object.CommandReward;
import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Example {

  public void giveItemReward(Player player) {
    ItemStack viewItem = new ItemStack(Material.ENDER_CHEST);
    ItemMeta meta = viewItem.getItemMeta();
    meta.setDisplayName("§6출석체크 §e보상");
    meta.setLore(Lists.newArrayList("§a클릭시 보상을 수령합니다."));
    viewItem.setItemMeta(meta);
    ItemStack coin = new ItemStack(Material.NETHER_STAR);
    ItemMeta coinMeta = coin.getItemMeta();
    coinMeta.setDisplayName("§6출석체크 §b코인");
    coin.setItemMeta(coinMeta);

    Reward reward = new ItemReward(viewItem, new ItemStack[]{coin});
    StorageBoxAPI.getInstance().give(player.getUniqueId(), reward);
  }

  public void giveCommandReward(Player player) {
    ItemStack viewItem = new ItemStack(Material.ENDER_CHEST);
    ItemMeta meta = viewItem.getItemMeta();
    meta.setDisplayName("§a퀘스트 §e보상");
    meta.setLore(Lists.newArrayList("§a클릭시 보상을 수령합니다."));
    viewItem.setItemMeta(meta);

    Reward reward = new CommandReward(viewItem, new String[]{ "보상 지급 %p 퀘스트1" });
    StorageBoxAPI.getInstance().give(player.getUniqueId(), reward);
  }
}
```