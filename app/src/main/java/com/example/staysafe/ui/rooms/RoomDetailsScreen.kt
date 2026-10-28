package com.example.staysafe.ui.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.staysafe.ui.login.HintGray
import com.example.staysafe.ui.login.ScreenBackground
import com.example.staysafe.ui.login.StayBlue
import com.example.staysafe.ui.login.StayBlueDark
import com.example.staysafe.ui.login.TextGray

private val PhotoPlaceholders = listOf(
    Color(0xFFBBD3F5),
    Color(0xFFC9DDF8),
    Color(0xFFADC8EF)
)

data class RoomFeature(val label: String, val icon: ImageVector)

@Composable
fun RoomDetailsScreen(
    roomNumber: String = "101",
    roomType: String = "Standard Room",
    hasWifi: Boolean = true,
    hasAirConditioning: Boolean = true,
    pricePerNight: Int = 45000,
    photoCount: Int = 3,
    onBackClick: () -> Unit = {},
    onReserveClick: () -> Unit = {}
) {
    // WiFi and AC are listed only if the room has them
    val features = buildList {
        add(RoomFeature("1 Bed", Icons.Outlined.Hotel))
        if (hasWifi) add(RoomFeature("Free WiFi", Icons.Outlined.Wifi))
        if (hasAirConditioning) add(RoomFeature("Air Conditioning", Icons.Outlined.AcUnit))
    }

    val pagerState = rememberPagerState(pageCount = { photoCount })

    Scaffold(containerColor = Color.White) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                // TODO: replace with real photos
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PhotoPlaceholders[page % PhotoPlaceholders.size]),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Hotel,
                            contentDescription = "Room photo ${page + 1}",
                            tint = StayBlue,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC1B2430))
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x99000000))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/$photoCount",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Room $roomNumber",
                    color = StayBlueDark,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = roomType, color = TextGray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    features.forEach { feature ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = null,
                                tint = StayBlueDark,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(text = feature.label, color = TextGray, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "MWK ${"%,d".format(pricePerNight)}",
                        color = StayBlueDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "/ night",
                        color = HintGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Button(
                onClick = onReserveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StayBlue)
            ) {
                Text(text = "Reserve Room", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun RoomDetailsScreenPreview() {
    RoomDetailsScreen()
}