package dev.parez.barz.sample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.parez.barz.sample.TEAM_CAPACITY
import dev.parez.barz.sample.TeamMember
import dev.parez.barz.sample.spriteUrlFor
import dev.parez.barz.sample.toDisplayName

@Composable
fun TeamScreen(
    members: List<TeamMember>,
    twentyFourHourTime: Boolean,
    onRemove: (Int) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding + PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(members, key = { it.id }, contentType = { "member" }) { member ->
            TeamCard(
                member = member,
                twentyFourHourTime = twentyFourHourTime,
                onRemove = { onRemove(member.id) },
            )
        }
        // The empty slots aren't drawn as placeholders — the design fills the remaining space with
        // a single invitation instead, whether the team is empty or merely not full.
        if (members.size < TEAM_CAPACITY) {
            item(contentType = "invitation") { EmptySlotsInvitation() }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeamCard(member: TeamMember, twentyFourHourTime: Boolean, onRemove: () -> Unit) {
    Surface(shape = CardShape, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                AsyncImage(
                    model = spriteUrlFor(member.id),
                    contentDescription = member.name,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(member.name.toDisplayName(), style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    member.types.forEach { TypeChip(it) }
                }
                Text(
                    text = "Added ${formatAddedAt(member.addedAt, twentyFourHourTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = onRemove,
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface,
                    ),
                // 28dp visual, 48dp target — this is a destructive control.
                modifier = Modifier.minimumInteractiveComponentSize().size(28.dp),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove ${member.name.toDisplayName()} from team",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptySlotsInvitation() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = spriteUrlFor(PIKACHU_ID),
            contentDescription = null,
            modifier = Modifier.size(140.dp).alpha(0.25f),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Add more pokemons\nto your team",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** The design's faded mascot on the empty state. */
private const val PIKACHU_ID = 25

@Composable
fun TeamFullDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = { PokeballMark(Modifier.size(64.dp)) },
        title = {
            Text(
                "Team is full!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                "Your team can only have $TEAM_CAPACITY Pokemons. " +
                    "Please, remove a member from your Team before adding a new one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = CircleShape,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface,
                    ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Close")
            }
        },
    )
}
