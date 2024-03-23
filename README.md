<div align="center">
<img src="https://github.com/KaatoDev/NotzCrates/assets/107152563/7bccf81a-1cfe-48e3-9eb2-c8c624f5fd99" alt="" height="320" >

#
NotzCrates é um plugin completo de Crates com: título em holograma, personalização de displayname e personalização de quantidade e chance de cada item.

## Informações
### `Crates`
As crates contam com um sistema de personalização de Material da raridade, virtualidade de keys (keys da crate poderem ser somente virtuais para evitar alguma forma de dupe);

Possibilidade de alterar a raridade, fácil adição, remoção e edição de recompensas e também deleção de crate;
### `Players`
O plugins salva os players como OfflinePlayers, então, mesmo que ocorra alguma alteração de crate, os inventários virtuais dos players também serão atualizados mesmo estando offline;
### Rewards
Com o novo sistema de rewards, as crates estão mais dinâmicas e otimizadas, mesmo que uma crate tenha 50 itens idênticos, cada um será único pois terá um id próprio dentro do sistema do plugin e na database.

</div>

## Dependências
- DecentHolograms

## Spoilers
- ### Loja

![Screenshot_213](https://github.com/KaatoDev/NotzShop/assets/107152563/2312a1bb-765c-4830-939f-30cec569212f)

- ### Carrinho

![Screenshot_215](https://github.com/KaatoDev/NotzShop/assets/107152563/5dad1461-0199-48db-96f5-3ec8104a39a7)

- ### Mochila

![Screenshot_216](https://github.com/KaatoDev/NotzShop/assets/107152563/836eab7d-0823-4832-bb67-4d0e55ea4c02)

## Permissões

- `notzcrates.admin` - Acesso aos comandos e funções de admin.
- `notzcrates.viewadmin` - Acesso aos comandos de ver o inventário de keys e rewards dos players.

## Commandos
 - `/crates` - Abre o menu de crates.

## Comandos `Admin`
### `/ncrates`
 - `create` \<Name> \<Display> \<Rarity> - Cria uma crate.
 - `getHoloRemover` - Recebe um removedor de holograma.
 - `isKey` - Identifica se o item atual na mão é uma Key.
 - `list` - Lista as crates.
 - `view` \<keys/rewards> \<Player> - Abre o inventário de keys ou rewards de um player para editar.
 - `<crate>` 
   - `clearLocations` - Reseta os locais onde há Crate setada.
   - `clearRewards` - Reseta as recompensas da Crate.
   - `disable/enable` - Habilita ou desabilita a Crate.
   - `get` - Recebe a Crate para ser colocada.
   - `key` (Player) - Dá ou recebe uma Key da Crate.
   - `keyall` - Dá uma Key da Crate para todos os players online.
   - `setDisplay` \<Display> - Altera o display da Crate.

### `Aliases`
- `/crates`:
  - /crate, /caixas, /caixa
- `/ncrates`
  - /ncrate, /nc
 
 ###### | <> argumento obrigatório. | () argumento opcional. |
 
#
###### Versões testadas: 1.8
