# ExampleMod
Forge入りMinecraft1.14.4のmoddingサンプルコード。(兼 備忘録)

## 対象バージョン
-Minecraft 1.14.4  
-Minecraft Forge 1.14.4-28.1.0 (10/25現在での最新安定版)

## 対象項目
* Item(アイテム)登録

* Block(ブロック)登録

* Container(コンテナ)登録
  - ContainerType

* TileEntity(タイルエンティティ)登録
  - TileEntityType

* Recipe(レシピ)登録
  - IRecipeType
  - IRecipeSerializer


* Item(アイテム)  
  - 個別クラス無しアイテム  
  - 個別クラス有りアイテム  
    - 食べ物  
    - 道具  

* Block(ブロック)  
  - 個別クラス無しブロック  
  - 個別クラス有りブロック  
    - ダメージブロック  
    - 食べ物(ケーキ)  
    - 作業台(Container, Screen, Recipe付き)
    - 加工機(TileEntity, Container, Screen, Recipe付き)  
    - チェスト(TileEntity, Container, Screen付き)
